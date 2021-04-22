import React, { useEffect, useState } from "react";
import { Grid, GridContainer, Table } from "@trussworks/react-uswds";

import { useFhir } from "../../context/FhirClientContext";
import { isBundle } from "../../@types";
import { notEmpty } from "../../components/VSMap/utils";

export const Sources: React.FC = () => {
  const { client } = useFhir();

  const [endpoints, setEndpoints] = useState<fhir.Endpoint[]>([]);

  useEffect(() => {
    const fetchData = async () => {
      const response = await client?.search({
        resourceType: "Endpoint",
      });

      if (isBundle(response)) {
        const endpoints = response.entry
          ?.map((entry) => entry.resource as fhir.Endpoint)
          .filter(notEmpty);
        if (endpoints) {
          setEndpoints(endpoints);
        }
      }
    };
    // noinspection JSIgnoredPromiseFromCall
    fetchData();
  });

  return (
    <GridContainer>
      <Grid row>
        <Table bordered={false}>
          <caption>Sources currently being synced</caption>
          <thead>
            <th scope="col">URL</th>
            <th scope="col">Status</th>
          </thead>
          <tbody>
            {endpoints.map((endpoint) => {
              return (
                <tr key={endpoint.id}>
                  <th scope="row">{endpoint.address}</th>
                  <th>{endpoint.status}</th>
                </tr>
              );
            })}
          </tbody>
        </Table>
      </Grid>
    </GridContainer>
  );
};
