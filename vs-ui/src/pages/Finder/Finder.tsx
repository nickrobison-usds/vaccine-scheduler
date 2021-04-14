import {Grid, GridContainer} from "@trussworks/react-uswds";
import React from "react";
import {Map} from "../../components/Map/Map";

export const Finder: React.FC<{}> = () => {


    return (
        <GridContainer>
            <Grid row>
                <Grid col={5}>
                    I'm a list
                </Grid>
                <Grid col={7}>
                    <Map/>
                </Grid>
            </Grid>
        </GridContainer>
    )
}
