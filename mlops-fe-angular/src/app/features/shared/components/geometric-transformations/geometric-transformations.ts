export interface GeometricTransformation {
  label: string;
  index: number;
  name: string;
}

// TODO: this is an information that must be shared with other services 
//  like the Airflow DAGs, the FatAPI microservices, the MLOps gateway 
//  backend. I believe that we could have a REST API endpoint sharing 
//  it in a JSON format.
export const GEOMETRIC_TRANSFORMATIONS: GeometricTransformation[] = [
  { name: 'SHEAR_X_IN_Y_DIRECTION', label: 'Shear X in Y direction', index: 1 },
  { name: 'SHEAR_X_IN_Z_DIRECTION', label: 'Shear X in Z direction', index: 2 },
  { name: 'SHEAR_Y_IN_X_DIRECTION', label: 'Shear Y in X direction', index: 3 },
  { name: 'SHEAR_Y_IN_Z_DIRECTION', label: 'Shear Y in Z direction', index: 4 },
  { name: 'SHEAR_Z_IN_X_DIRECTION', label: 'Shear Z in X direction', index: 5 },
  { name: 'SHEAR_Z_IN_Y_DIRECTION', label: 'Shear Z in Y direction', index: 6 },
  { name: 'TENSION_X', label: 'Tension along X', index: 7 },
  { name: 'COMPRESSION_X', label: 'Compression along X', index: 8 },
  { name: 'TENSION_Y', label: 'Tension along Y', index: 9 },
  { name: 'COMPRESSION_Y', label: 'Compression along Y', index: 10 },
  { name: 'TENSION_Z', label: 'Tension along Z', index: 11 },
  { name: 'COMPRESSION_Z', label: 'Compression along Z', index: 12 },
  { name: 'TENSION_XYZ', label: 'Tension along X, Y, and Z', index: 13 },
  { name: 'COMPRESSION_XYZ', label: 'Compression along X, Y, and Z', index: 14 },
];
