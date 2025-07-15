/*
 * Angular equivalent model in TypeScript of a DTO for SubRuns.
 * 
 * NOTE: It could be a class if there was the need for methods or 
 *  forinstantiating objects with logic.
 */
export interface SubRun {

  id: number;
  sub_run_number: number;
  created_at: string;
  updated_at: string;
  created_by: string;
  updated_by: string;

}
