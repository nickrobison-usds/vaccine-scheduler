import {
  faBan,
  faCheckCircle,
  faExclamationCircle,
} from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  Card,
  CardBody,
  CardFooter,
  CardHeader,
} from "@trussworks/react-uswds";
import React from "react";

import "./LocationCard.scss";
import { CapitatedLocation } from "../../@types";

export interface LocationCardProps {
  location: CapitatedLocation;
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

const LocationFooter: React.FC<LocationCardProps> = ({ location }) => {
  const { capacity, link } = location;
  const buildIcon = (capacity: number | undefined) => {
    if (capacity) {
      switch (capacity) {
        case 0:
          return faBan;
        default:
          return faCheckCircle;
      }
    } else {
      return faExclamationCircle;
    }
  };

  return (
    <CardFooter className="location-card--footer">
      <FontAwesomeIcon icon={buildIcon(capacity)} />
      {capacity ? `${capacity} slots available` : "Unknown availability"}
      {link && <div>Book here</div>}
    </CardFooter>
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
      </CardHeader>
      <CardBody>{address && <LocationAddress address={address} />}</CardBody>
      <LocationFooter location={location} />
    </Card>
  );
};
