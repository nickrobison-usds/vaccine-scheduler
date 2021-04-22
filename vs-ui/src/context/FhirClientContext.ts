import { createContext, useContext } from "react";

import { OperationClient } from "../@types";

export type FhirContextType = {
  client?: OperationClient;
};

export const FhirClientContext = createContext<FhirContextType>({});
export const useFhir = () => useContext(FhirClientContext);
