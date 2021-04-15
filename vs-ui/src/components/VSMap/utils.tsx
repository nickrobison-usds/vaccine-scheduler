import { useEffect, useState } from "react";

export const usePosition = () => {
  const [position, setPosition] = useState<GeolocationPosition | null>(null);
  const [error, setError] = useState<string | null>(null);

  const onChange = (position: GeolocationPosition) => {
    setPosition(position);
  };

  const onError = (error: GeolocationPositionError) => {
    setError(error.message);
  };

  useEffect(() => {
    const geo = navigator.geolocation;
    if (!geo) {
      setError("Geolocation is not supported");
      return;
    }
    geo.getCurrentPosition(onChange, onError);
  }, []);

  return { ...position, error };
};

export function notEmpty<TValue>(
  value: TValue | null | undefined
): value is TValue {
  if (value === null || value === undefined) return false;
  // noinspection JSUnusedLocalSymbols
  // eslint-disable-next-line
  const testDummy: TValue = value;
  return true;
}
