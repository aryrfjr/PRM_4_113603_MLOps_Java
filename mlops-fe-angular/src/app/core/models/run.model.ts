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
  created_at: string;
  updated_at: string;
  created_by: string;
  updated_by: string;
  started_at: string;
  completed_at: string;
  
}
