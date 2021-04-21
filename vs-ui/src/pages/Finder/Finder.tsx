import {
  CardGroup,
  Dropdown,
  Grid,
  GridContainer,
} from "@trussworks/react-uswds";
import React, { ChangeEvent, useEffect, useState } from "react";
import flow from "lodash/fp/flow";
import { groupBy, map } from "lodash/fp";

import { useFhir } from "../../context/FhirClientContext";
import { CapitatedLocation, isBundle } from "../../@types";
import { LocationCard } from "../../components/LocationCard/LocationCard";
import "./Finder.scss";
import { VSMap } from "../../components/VSMap/VSMap";
import { notEmpty } from "../../components/VSMap/utils";

export const Finder: React.FC = () => {
  const { client } = useFhir();
  const [locations, setLocations] = useState<CapitatedLocation[]>([]);
  const [state, setState] = useState<string | null>(null);

  useEffect(() => {
    if (state !== null) {
      const fetchData = async () => {
        const response = await client?.search({
          resourceType: "Location",
          searchParams: {
            "address-state": state,
          },
        });

        if (isBundle(response)) {
          console.debug("Response: ", response);
          const locations = response.entry?.map((entry) => {
            return entry.resource as CapitatedLocation;
          });
          if (locations) {
            // Pull out the location IDs
            const locIds = locations
              .map((loc) => loc.id)
              .filter(notEmpty)
              .join(",");

            // Generate a map of locations to update resources for
            const locMap = locations.reduce<{
              [id: string]: CapitatedLocation;
            }>((map, loc) => {
              map[loc.id!] = loc;
              return map;
            }, {});

            const slotBundle = await client?.search({
              resourceType: "Slot",
              searchParams: {
                "schedule.actor": locIds,
                start: `ge${new Date(2021, 2, 1).toISOString()}`,
                _include: "Slot:schedule",
              },
            });

            if (isBundle(slotBundle)) {
              console.debug("I have slots: ", slotBundle);

              // Ok, do the slot things, group them up, count the available slots and then update the locationMap
              const slots = flow(
                map<fhir.BundleEntry, fhir.Slot>(
                  (entry) => entry.resource as fhir.Slot
                ),
                groupBy(
                  (slot) =>
                    slot
                      .extension!.find(
                        (e) => e.url === "http://usds.gov/vaccine/forLocation"
                      )!
                      .valueId!.split("/")[1]
                )
              )(slotBundle.entry!);

              Object.keys(slots).forEach((key) => {
                const vals = slots[key];
                const totalCapacity = vals.reduce<number>((acc, slot) => {
                  let capacity = slot.extension?.find(
                    (ext) =>
                      ext.url ===
                      "http://fhir-registry.smarthealthit.org/StructureDefinition/slot-capacity"
                  )?.valueInteger;
                  capacity = capacity ? capacity : 0;
                  return acc + capacity;
                }, 0);
                const maybeLocation = locMap[key];
                if (maybeLocation) {
                  maybeLocation.capacity = totalCapacity;
                }
              });
              setLocations(Object.values(locMap));
            }
          }
        }
      };
      // noinspection JSIgnoredPromiseFromCall
      fetchData();
    }
  }, [state, client]);

  const handleSelectChange = (e: ChangeEvent<HTMLSelectElement>) => {
    setState(e.target.value);
  };

  return (
    <GridContainer>
      <Grid row className="finder-row">
        <Dropdown id="state" name="state" onChange={handleSelectChange}>
          <option> - Select -</option>
          {/*Yet another hack to work around lack of normalized addresses. Probably something we can push into the WA publisher*/}
          <option value="Washington">Washington</option>
          <option value="NJ">New Jersey</option>
          <option value="MA">Massachusetts</option>
        </Dropdown>
      </Grid>
      <Grid row className="finder-row">
        <Grid col={5}>
          <CardGroup>
            {locations.map((loc) => {
              return <LocationCard location={loc} key={loc.id} />;
            })}
          </CardGroup>
        </Grid>
        <Grid col={7}>
          <VSMap
            features={locations
              .map((l) => {
                if (l.position) {
                  return {
                    lng: l.position.longitude,
                    lat: l.position.latitude,
                    id: l.id!,
                    title: l.name!,
                  };
                }
                return null;
              })
              .filter(notEmpty)}
          />
        </Grid>
      </Grid>
    </GridContainer>
  );
};
