import { render, screen } from "@testing-library/react";

import { Finder } from "./Finder";

describe("Finder", () => {
  it("renders text", async () => {
    const { container } = render(<Finder />);
    expect(screen.getByTestId("dropdown")).toBeInTheDocument();
    expect(container).toMatchSnapshot();
  });
});
