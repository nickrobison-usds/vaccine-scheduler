import {Button, Textarea} from "@trussworks/react-uswds";
import {Field, Form, Formik} from "formik";
import React from "react";

interface FormValues {
    text: string
}

export const Validate: React.FC<{}> = () => {
    return (
        <Formik
            initialValues={{
                text: '',
            }}
            onSubmit={(values: FormValues) => {
                console.debug("I have values: ", values);
            }}
        >
            <Form>
                <label htmlFor="text">Paste data here:</label>
                <Field id="text" as={Textarea} name="text" placeholder="Paste data here"/>
                <Button type="submit">Validate</Button>
            </Form>
        </Formik>
    )
}
