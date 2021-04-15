import {
  CardGroup,
  Grid,
  GridContainer,
  Search,
} from "@trussworks/react-uswds";
import React, { FormEvent, useEffect, useState } from "react";

import { useFhir } from "../../context/FhirClientContext";
import { isBundle } from "../../@types";
import { LocationCard } from "../../components/LocationCard/LocationCard";
import "./Finder.scss";
import { VSMap } from "../../components/VSMap/VSMap";
import { notEmpty } from "../../components/VSMap/utils";

export const Finder: React.FC = () => {
  const { client } = useFhir();
  const [locations, setLocations] = useState<fhir.Location[]>([]);

  const handleChange = (text: FormEvent<HTMLElement>) =>
    console.debug("Changed to: ", text);
  const handleSubmit = (text: FormEvent<HTMLElement>) =>
    console.debug("Submitting: ", text);

  useEffect(() => {
    const fetchData = async () => {
      const response = await client?.search({
        resourceType: "Location",
      });

      if (isBundle(response)) {
        console.debug("Response: ", response);
        const locations = response.entry?.map((entry) => {
          return entry.resource as fhir.Location;
        });
        if (locations) {
          setLocations(locations);
        }
      }
    };
    // noinspection JSIgnoredPromiseFromCall
    fetchData();
  });

  return (
    <GridContainer>
      <Grid row className="finder-row">
        <Search onChange={handleChange} onSubmit={handleSubmit} />
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
