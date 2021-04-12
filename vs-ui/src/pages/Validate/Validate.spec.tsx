import {render, screen} from "@testing-library/react";
import {Validate} from "./Validate";

describe('Finder', () => {
    it('renders text', async () => {
        const {container} = render(<Validate/>);
        expect(await screen.getByText(/data here/i)).toBeInTheDocument();
        expect(container).toMatchSnapshot();
    })
});
