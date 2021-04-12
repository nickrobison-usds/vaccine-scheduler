import {render, screen} from "@testing-library/react";
import {Finder} from "./Finder";

describe('Finder', () => {
    it('renders text', async () => {
        render(<Finder/>);
        expect(await screen.getByText(/finder page/i)).toBeInTheDocument();
    });
})
