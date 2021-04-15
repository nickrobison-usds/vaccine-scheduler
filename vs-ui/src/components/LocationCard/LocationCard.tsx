import { faExclamationTriangle } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  Card,
  CardBody,
  CardFooter,
  CardHeader,
} from "@trussworks/react-uswds";
import React from "react";
import "./LocationCard.scss";

export interface LocationCardProps {
  location: fhir.Location;
}

const LocationAddress: React.FC<{ address: fhir.Address }> = ({ address }) => {
  return (
    <div>
      {address.line![0]}
      <br />
      {address.city!}, {address.state!} {address.postalCode!}
    </div>
  );
};

export const LocationCard: React.FC<LocationCardProps> = ({ location }) => {
  const address = location.address;

  return (
    <Card className="location-card">
      <CardHeader>
        <h3 className="usa-card__heading">
          {location.name ? location.name : ""}
        </h3>
        <CardBody>{address && <LocationAddress address={address} />}</CardBody>
        <CardFooter className="location-card--footer">
          <FontAwesomeIcon icon={faExclamationTriangle} />
          <span />
          100 slots available
        </CardFooter>
      </CardHeader>
    </Card>
  );
};
