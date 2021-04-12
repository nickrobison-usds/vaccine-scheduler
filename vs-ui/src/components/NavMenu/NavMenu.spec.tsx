import {NavMenu} from "./NavMenu";
import {render, screen} from "@testing-library/react";
import React from "react";
import {MemoryRouter} from "react-router-dom";

const TestContainer: React.FC = ({children}) => (
    <MemoryRouter>
        <>{children}</>
    </MemoryRouter>
);

describe('NavMenu', () => {
    it('renders correctly', () => {
        const {container} = render(<TestContainer><NavMenu/></TestContainer>);
        expect(screen.getByText('Finder')).toBeInTheDocument();
        expect(container).toMatchSnapshot();
    })
});
