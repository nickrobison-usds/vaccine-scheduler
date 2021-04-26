import { render } from "@testing-library/react";

import { GlobalAlert } from "./GlobalAlert";

describe("GlobalAlert", () => {
  it("renders with icon", () => {
    const { container } = render(
      <GlobalAlert typ="warning" text="icon" icon={true} />
    );
    expect(container).toMatchSnapshot();
  });
  it("renders without an icon", () => {
    const { container } = render(
      <GlobalAlert typ="warning" text="no icon" icon={false} />
    );
    expect(container).toMatchSnapshot();
  });
});
