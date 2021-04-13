import {GridContainer} from '@trussworks/react-uswds';
import React from 'react';
import {BrowserRouter, Route, Switch} from 'react-router-dom';
import './App.css';
import {NavMenu} from "./components/NavMenu/NavMenu";
import {Home} from "./pages/Home/Home";
import {Validate} from "./pages/Validate/Validate";
import {Finder} from "./pages/Finder/Finder";
import {FhirClientProvider} from "./providers/FhirClientProvider";

function App() {
    return (
        <BrowserRouter>
            <FhirClientProvider>
                <div className="App">
                    <NavMenu/>
                    <section className="usa-section">
                        <GridContainer>
                            <Switch>
                                <Route exact path="/" component={Home}/>
                                <Route path="/validate" component={Validate}/>
                                <Route path="/finder" component={Finder}/>
                            </Switch>
                        </GridContainer>
                    </section>
                </div>
            </FhirClientProvider>
        </BrowserRouter>
    );
}

export default App;
