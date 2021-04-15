import React from "react";

import * as Geo from "geojson";
import ReactMapboxGl, { Feature, Layer } from "react-mapbox-gl";
import "mapbox-gl/dist/mapbox-gl.css";

export interface MapFeature {
  lng: number;
  lat: number;
  id: string;
  title: string;
}

export interface MapProps {
  features: MapFeature[];
}

const transformFeature = (feature: MapFeature): Geo.Feature => {
  return {
    type: "Feature",
    id: feature.id,
    geometry: {
      type: "Point",
      coordinates: [feature.lng, feature.lat],
    },
    properties: {
      title: feature.title,
    },
  };
};

export const VSMap: React.FC<MapProps> = (props) => {
  const { features } = props;

  const Map = ReactMapboxGl({
    accessToken: process.env.REACT_APP_MAPBOX_TOKN!,
  });

  console.debug("Map, render, ", features);

  return (
    <Map
      style="mapbox://styles/mapbox/streets-v9"
      center={[-0.09, 51.505]}
      containerStyle={{
        height: "100vh",
        width: "50w",
      }}
    >
      <Layer
        type="symbol"
        id="marker"
        layout={{ "icon-image": "marker-15", "icon-size": 100 }}
      >
        <Feature coordinates={[-0.09, 51.505]} />
      </Layer>
    </Map>
  );
};
