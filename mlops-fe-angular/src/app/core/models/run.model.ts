/*
 * Angular equivalent model in TypeScript of a DTO for Runs.
 * 
 * NOTE: It could be a class if there was the need for methods or 
 *  forinstantiating objects with logic.
 */
export interface Run {

  id: number;
  status: string;
  run_number: number;
  created_by: string;
  updated_by: string;
  created_at: string;
  updated_at: string;
  
}
