import React from "react";
import GoogleMapReact, { Coords } from "google-map-react";
import { faMapMarker } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

export interface MapFeature {
  lng: number;
  lat: number;
  id: string;
  title: string;
}

export interface MapProps {
  features: MapFeature[];
}

const LocationPin: React.FC<Coords & { text: string }> = ({ text }) => (
  <div className="pin">
    <FontAwesomeIcon icon={faMapMarker} />
    <p className="pin-text">{text}</p>
  </div>
);

export const VSMap: React.FC<MapProps> = (props) => {
  const { features } = props;

  return (
    <div style={{ height: "100vh", width: "100%" }}>
      <GoogleMapReact
        bootstrapURLKeys={{ key: process.env.REACT_APP_GOOGLE_TOKEN! }}
        center={{
          lat: 42.35,
          lng: -70.9,
        }}
        zoom={11}
      >
        {features &&
          features.map((feature) => {
            return (
              <LocationPin
                key={feature.id}
                lat={feature.lat}
                lng={feature.lng}
                text={feature.title}
              />
            );
          })}
      </GoogleMapReact>
    </div>
  );
};
