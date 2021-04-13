import {act, fireEvent, render, screen} from "@testing-library/react";
import {Validate} from "./Validate";
import {FhirClientContext} from "../../context/FhirClientContext";
import {FhirClientError, OperationClient} from "../../@types";
import Client from "fhir-kit-client";

jest.mock('fhir-kit-client');

const slotFailure: FhirClientError = {
    config: undefined,
    response: {
        status: 422,
        data: {
            "resourceType": "OperationOutcome",
            "issue": [
                {
                    "severity": "error",
                    "code": "processing",
                    "diagnostics": "Slot.schedule: minimum required = 1, but only found 0 (from http://fhir-registry.smarthealthit.org/StructureDefinition/vaccine-slot)",
                    "location": [
                        "Slot",
                        "Line 1, Col 35"
                    ]
                },
                {
                    "severity": "error",
                    "code": "processing",
                    "diagnostics": "Slot.status: minimum required = 1, but only found 0 (from http://fhir-registry.smarthealthit.org/StructureDefinition/vaccine-slot)",
                    "location": [
                        "Slot",
                        "Line 1, Col 35"
                    ]
                },
                {
                    "severity": "error",
                    "code": "processing",
                    "diagnostics": "Slot.start: minimum required = 1, but only found 0 (from http://fhir-registry.smarthealthit.org/StructureDefinition/vaccine-slot)",
                    "location": [
                        "Slot",
                        "Line 1, Col 35"
                    ]
                },
                {
                    "severity": "error",
                    "code": "processing",
                    "diagnostics": "Slot.end: minimum required = 1, but only found 0 (from http://fhir-registry.smarthealthit.org/StructureDefinition/vaccine-slot)",
                    "location": [
                        "Slot",
                        "Line 1, Col 35"
                    ]
                }
            ]
        }
    }

};

// const mockClient = jest.fn();
//
// mockClient.mockImplementation(() => {
//     return {
//
//     }
// })

// const mockClient = jest.mock("fhir-kit-client", () => {
//     return jest.fn().mockImplementation(() => {
//         return {
//             operation: (params: any) => {
//                 console.debug("Does this work?");
//                 return Promise.reject("nope");
//             }
//         }
//     })
// })

describe('Validate', () => {
    it('renders text', async () => {
        const {container} = render(<Validate/>);
        expect(await screen.getByText(/text here/i)).toBeInTheDocument();
        expect(container).toMatchSnapshot();
    });

    it('handles non-FHIR JSON', async () => {
        render(<Validate/>);
        act(() => {
            fireEvent.input(screen.getByLabelText('validation-text'), {
                target: {
                    value: "{\"resz\": \"Slot\", \"id\": 1}"
                }
            });
        });

        act(() => {
            fireEvent.submit(screen.getByRole("button", {name: /validate/i}));
        });
        expect(await screen.findByText("Not a fhir resource")).toBeInTheDocument();
    })

    it('displays validation errors', async () => {
        // @ts-ignore
        Client.mockImplementationOnce(() => {
            return {
                operation: () => Promise.reject(slotFailure),
            };
        });
        const client = new Client({baseUrl: 'http://nothing'}) as OperationClient;
        const {container} = render(
            <FhirClientContext.Provider value={{client}}>
                <Validate/>
            </FhirClientContext.Provider>
        );

        act(() => {
            fireEvent.input(screen.getByLabelText('validation-text'), {
                target: {
                    value: "{\"resourceType\": \"Slot\", \"id\": 1}"
                }
            });
        });

        act(() => {
            fireEvent.submit(screen.getByRole("button", {name: /validate/i}));
        });
        expect(container).toMatchSnapshot();
    })
});
