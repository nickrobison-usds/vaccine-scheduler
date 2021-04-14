import React, {useEffect, useRef, useState} from "react";
import mapboxgl from "mapbox-gl";
import './Map.scss';
import "mapbox-gl/dist/mapbox-gl.css";
import {usePosition} from "./utils";

console.debug("Token: ", process.env.REACT_APP_MAPBOX_TOKEN);
mapboxgl.accessToken = process.env.REACT_APP_MAPBOX_TOKEN!;

export const Map: React.FC<{}> = () => {

    let map: mapboxgl.Map;

    const mapContainer = useRef<HTMLDivElement>(null);
    const [lng, setLng] = useState(-70.9);
    const [lat, setLat] = useState(42.35);
    const [zoom, setZoom] = useState(9);

    const {coords, error} = usePosition();

    useEffect(() => {
        console.debug("I have a change", coords, error);
        if (!coords) {
            return;
        }
        console.debug("Setting new position", coords);
        map.setCenter({
            lng: coords.longitude,
            lat: coords.latitude
        });
    }, [coords])

    useEffect(() => {
        map = new mapboxgl.Map({
            container: mapContainer.current!,
            style: 'mapbox://styles/mapbox/streets-v11',
            center: [lng, lat],
            zoom: zoom
        });

        map.on('move', () => {
            setLng(Number.parseFloat(map.getCenter().lng.toFixed(4)));
            setLat(Number.parseFloat(map.getCenter().lat.toFixed(4)));
            setZoom(Number.parseFloat(map.getZoom().toFixed(2)));
        });
        return () => map.remove();
    }, []); // This violates exhaustive deps check, can we work around it?
    return (
        <div>
            <div className="map-container" ref={mapContainer}/>
        </div>
    )
}
