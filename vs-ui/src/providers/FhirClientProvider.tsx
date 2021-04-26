import React, { useEffect, useState } from "react";
import Client from "fhir-kit-client";

import { FhirClientContext } from "../context/FhirClientContext";
import { OperationClient } from "../@types";
import { AlertType, GlobalAlert } from "../components/GlobalAlert/GlobalAlert";

interface ConnectionStatus {
  text: string;
  typ: AlertType;
}

export const FhirClientProvider: React.FC = ({ children }) => {
  const [fc] = useState(
    new Client({ baseUrl: `${process.env.REACT_APP_BACKEND_URL}/fhir` })
  );

  const [status, setStatus] = useState<ConnectionStatus | null>(null);

  useEffect(() => {
    const checkConnection = async () => {
      // Wait 3 seconds before showing a notification
      const timer = setTimeout(() => {
        setStatus({
          text: "Waking the beast (May take a while)",
          typ: "info",
        });
      }, 3000);
      try {
        await fc.capabilityStatement();
        setStatus(null);
      } catch (e) {
        setStatus({
          text: "Unable to connect to backend",
          typ: "error",
        });
      } finally {
        // When the request finishes, make sure we clear the timeout so we don't fire it later.
        clearTimeout(timer);
      }
    };
    // noinspection JSIgnoredPromiseFromCall
    checkConnection();
  }, [fc]);

  return (
    <FhirClientContext.Provider value={{ client: fc as OperationClient }}>
      <FhirClientContext.Consumer>
        {({ client }) => {
          if (client) {
            return [
              status && (
                <GlobalAlert typ={status.typ} text={status.text} icon={true} />
              ),
              children,
            ];
          }
          return "Authorizing...";
        }}
      </FhirClientContext.Consumer>
    </FhirClientContext.Provider>
  );
};
