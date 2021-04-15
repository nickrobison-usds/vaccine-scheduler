import Client from "fhir-kit-client";

export interface OperationParams<T extends fhir.DomainResource> {
    name: string;
    resourceType?: string;
    id?: string;
    method?: "get" | "post";
    input?: string | T;
}

export interface FhirClientResponse {
    status: number;
    data: fhir.OperationOutcome;
}

export interface FhirClientError {
    config: any; // Type this
    response: FhirClientResponse;
}

export interface OperationClient extends Client {
    operation<T extends fhir.DomainResource>(params: OperationParams<T>): Promise<fhir.OperationOutcome>;
}

// Guards

export function isClientError(err: any): err is FhirClientError {
    return (err as FhirClientError).response !== undefined;
}

export function isFHIRResource(json: any): json is fhir.DomainResource {
    return (json as fhir.DomainResource).resourceType !== undefined;
}

export function isBundle(bundle: any): bundle is fhir.Bundle {
    return (bundle as fhir.Bundle).type !== undefined;
}
