/*
 * Angular equivalent model in TypeScript of a DTO for Runs.
 * 
 * NOTE: It could be a class if there was the need for methods or 
 *  forinstantiating objects with logic.
 */
export interface Run {

  id: number;
  run_number: number;
  status: string;
  created_at: string;  // TODO: may want to parse to Date
  completed_at: string;
  
}
