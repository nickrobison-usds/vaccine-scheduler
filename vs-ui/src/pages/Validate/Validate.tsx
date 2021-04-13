import {Alert, Button, ErrorMessage, Grid, Label, Textarea} from "@trussworks/react-uswds";
import {Field, Form, FormikBag, FormikProps, withFormik} from "formik";
import React, {useState} from "react";
import {useFhir} from "../../context/FhirClientContext";
import {isClientError, isFHIRResource} from "../../@types";

interface FormValues {
    text: string
}

const severityToAlertType = (severity: fhir.IssueSeverity): "error" | "warning" | "success" | "info" => {
    switch (severity) {
        case "error":
            return "error";
        default:
            return "error"
    }
}

const issueToAlert = (issue: fhir.OperationOutcomeIssue) => {
    return (
        <Alert type={severityToAlertType(issue.severity)} slim noIcon>
            {issue.diagnostics}
        </Alert>
    )
}

const ValidationForm = (props: FormikProps<FormValues>) => {
    const {
        touched,
        errors,
        handleSubmit,
        isSubmitting
    } = props;
    return (<Form onSubmit={handleSubmit} className="usa-form">
        <Label htmlFor="text">Paste text here:</Label>
        <Field id="text" as={Textarea} name="text" placeholder="Paste data here" aria-label="validation-text"/>
        {touched.text && errors.text && <ErrorMessage>{errors.text}</ErrorMessage>}
        <Button type="submit" disabled={isSubmitting}>Validate</Button>
    </Form>)
}

export const Validate: React.FC<{}> = () => {
    const {client} = useFhir();
    const [validationErrors, setValidationErrors] = useState<fhir.OperationOutcomeIssue[]>([]);

    const handleSubmission = async (values: FormValues, bag: FormikBag<any, any>) => {
        const {setFieldError} = bag;
        console.debug("I have values: ", values);

        // JSON stringify
        const maybeFHIR = JSON.parse(values.text);

        if (!isFHIRResource(maybeFHIR)) {
            // We need to handle this better
            setFieldError('text', 'Not a fhir resource');
            return;
        }
        try {
            const res = await client?.operation({
                name: 'validate',
                resourceType: 'Slot',
                input: maybeFHIR
            });
            console.debug("Response: ", res);
        } catch (exn) {
            if (isClientError(exn)) {
                console.error("Validation failed: ", exn.response.data);
                setValidationErrors(exn.response.data.issue);
            } else {
                console.error("Something else went wrong: ", exn);
            }
        }
    };

    const FormikForm = withFormik({
        displayName: 'ValidationForm',
        handleSubmit: handleSubmission,
    })(ValidationForm);

    return (
        <Grid>
            <Grid row={true}>
                <FormikForm/>
            </Grid>
            <Grid row={true}>
                <section className="usa-section">
                    {validationErrors.map(issueToAlert)}
                </section>
            </Grid>
        </Grid>
    )
}
