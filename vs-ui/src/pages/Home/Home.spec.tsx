import {render, screen} from "@testing-library/react";
import {Home} from "./Home";

describe('Home', () => {
    it('renders text', async () => {
        render(<Home/>);
        expect(await screen.getByText(/home page/i)).toBeInTheDocument();
    })
});
