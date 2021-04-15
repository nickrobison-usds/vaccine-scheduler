import React from "react";
import { render, screen } from "@testing-library/react";

import App from "./App";

describe("App", () => {
  it("renders header link", () => {
    render(<App />);
    const linkElement = screen.getByText(/VaccineScheduler/i);
    expect(linkElement).toBeInTheDocument();
  });
});
