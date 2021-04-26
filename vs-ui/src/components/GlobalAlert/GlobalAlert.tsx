import { Alert } from "@trussworks/react-uswds";
import React from "react";
import "./GlobalAlert.scss";

export type AlertType = "success" | "warning" | "error" | "info";

export interface AlertProps {
  typ: AlertType;
  text: string;
  icon: boolean;
}

export const GlobalAlert: React.FC<AlertProps> = (props) => {
  return (
    <Alert type={props.typ} noIcon={!props.icon} className="vs-alert">
      {props.text}
    </Alert>
  );
};
