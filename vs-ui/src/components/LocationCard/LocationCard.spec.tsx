import { render } from "@testing-library/react";
import React from "react";

import { CapitatedLocation } from "../../@types";

import { LocationCard } from "./LocationCard";

describe("LocationCare", () => {
  it("renders with available slots", () => {
    const location: CapitatedLocation = {
      id: "test-id",
      address: {
        line: ["1234 Test Avenue"],
        city: "Washington",
        state: "DC",
        postalCode: "20008",
      },
      capacity: 100,
    };
    const { container } = render(<LocationCard location={location} />);
    expect(container).toMatchSnapshot();
  });

  it("renders with no available slots", () => {
    const location: CapitatedLocation = {
      id: "test-id",
      address: {
        line: ["1234 Test Avenue"],
        city: "Washington",
        state: "DC",
        postalCode: "20008",
      },
      capacity: 0,
    };
    const { container } = render(<LocationCard location={location} />);
    expect(container).toMatchSnapshot();
  });

  it("renders with unknown slots", () => {
    const location: CapitatedLocation = {
      id: "test-id",
      address: {
        line: ["1234 Test Avenue"],
        city: "Washington",
        state: "DC",
        postalCode: "20008",
      },
    };
    const { container } = render(<LocationCard location={location} />);
    expect(container).toMatchSnapshot();
  });
});
