import React from "react";
import { BrowserRouter, Route, Switch } from "react-router-dom";

import "./App.css";
import { NavMenu } from "./components/NavMenu/NavMenu";
import { Home } from "./pages/Home/Home";
import { Validate } from "./pages/Validate/Validate";
import { Finder } from "./pages/Finder/Finder";
import { FhirClientProvider } from "./providers/FhirClientProvider";

function App() {
  return (
    <BrowserRouter>
      <FhirClientProvider>
        <div className="App">
          <div id="main-wrapper">
            <NavMenu />
            <Switch>
              <Route exact path="/" component={Home} />
              <Route path="/validate" component={Validate} />
              <Route path="/finder" component={Finder} />
            </Switch>
          </div>
        </div>
      </FhirClientProvider>
    </BrowserRouter>
  );
}

export default App;