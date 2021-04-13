import {useState} from "react";
import {FhirClientContext} from "../context/FhirClientContext";
import Client from "fhir-kit-client";
import {OperationClient} from "../@types";


export const FhirClientProvider: React.FC = ({children}) => {
    const [fc] = useState(new Client({baseUrl: 'http://localhost:8080/fhir'}))
    return (
        <FhirClientContext.Provider value={{client: fc as OperationClient}}>
            <FhirClientContext.Consumer>
                {({client}) => {
                    if (client) {
                        return children;
                    }
                    return "Authorizing...";
                }}
            </FhirClientContext.Consumer>

        </FhirClientContext.Provider>
    )
}
