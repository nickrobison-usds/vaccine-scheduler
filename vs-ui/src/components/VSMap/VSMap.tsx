import React, { useEffect, useState } from "react";
import GoogleMapReact, {
  Coords,
  fitBounds,
  NESWBounds,
} from "google-map-react";
import { faMapMarker } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import bbox from "@turf/bbox";

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

  const [center, setCenter] = useState<Coords | undefined>(undefined);
  const [zoom, setZoom] = useState<number>(11);

  useEffect(() => {
    if (features.length === 0) {
      return;
    }

    const geoFeatures = features
      .map((feature) => ({
        type: "Point",
        coordinates: [feature.lng, feature.lat],
      }))
      .map((point) => ({
        type: "Feature",
        geometry: point,
      }));

    const box = bbox({
      type: "FeatureCollection",
      features: geoFeatures,
    });

    const mapBounds: NESWBounds = {
      ne: {
        lat: box[3],
        lng: box[2],
      },
      sw: {
        lat: box[1],
        lng: box[0],
      },
    };
    // This is a hack, what's the actual size?
    const size = {
      width: 640,
      height: 380,
    };

    const { center, zoom } = fitBounds(mapBounds, size);
    setCenter(center);
    setZoom(zoom);
  }, [features]);

  return (
    <div style={{ height: "100vh", width: "100%" }}>
      <GoogleMapReact
        bootstrapURLKeys={{ key: process.env.REACT_APP_GOOGLE_TOKEN! }}
        defaultCenter={{
          lat: 42.35,
          lng: -70.9,
        }}
        defaultZoom={11}
        center={center}
        zoom={zoom}
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
