import { useState } from "react";
import Client from "fhir-kit-client";

import { FhirClientContext } from "../context/FhirClientContext";
import { OperationClient } from "../@types";

export const FhirClientProvider: React.FC = ({ children }) => {
  const [fc] = useState(
    new Client({ baseUrl: `${process.env.REACT_APP_BACKEND_URL}/fhir` })
  );
  return (
    <FhirClientContext.Provider value={{ client: fc as OperationClient }}>
      <FhirClientContext.Consumer>
        {({ client }) => {
          if (client) {
            return children;
          }
          return "Authorizing...";
        }}
      </FhirClientContext.Consumer>
    </FhirClientContext.Provider>
  );
};
