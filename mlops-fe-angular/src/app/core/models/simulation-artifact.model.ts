/*
 * Angular equivalent model in TypeScript of a DTO for SimulationArtifacts.
 * 
 * NOTE: It could be a class if there was the need for methods or 
 *  forinstantiating objects with logic.
 */
export interface SimulationArtifact {

    id: number;
    artifact_type: string;
    file_path: string;
    file_size: string;
    checksum: string;
  
}
