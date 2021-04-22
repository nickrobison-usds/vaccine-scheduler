import { Link, NavLink } from "react-router-dom";
import { Header, PrimaryNav, Title } from "@trussworks/react-uswds";
import React from "react";

export const NavMenu: React.FC<{}> = () => {
  const navItems = [
    <NavLink key="home" to="/">
      Home
    </NavLink>,
    <NavLink key="validate" to="/validate">
      Validate
    </NavLink>,
    <NavLink key="finder" to="/finder">
      Finder
    </NavLink>,
  ];

  return (
    <Header basic>
      <div className="usa-nav-container">
        <div className="usa-navbar">
          <Title>
            <Link to="/">VaccineScheduler</Link>
          </Title>
        </div>
        <PrimaryNav aria-label="Primary navigation" items={navItems} />
      </div>
    </Header>
  );
};
