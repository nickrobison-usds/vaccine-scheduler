import {
  CardGroup,
  Grid,
  GridContainer,
  Search,
} from "@trussworks/react-uswds";
import React, { FormEvent, useEffect, useState } from "react";

import { Map } from "../../components/Map/Map";
import { useFhir } from "../../context/FhirClientContext";
import { isBundle } from "../../@types";
import { LocationCard } from "../../components/LocationCard/LocationCard";
import "./Finder.scss";

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
              return <LocationCard location={loc} />;
            })}
          </CardGroup>
        </Grid>
        <Grid col={7}>
          <Map />
        </Grid>
      </Grid>
    </GridContainer>
  );
};
