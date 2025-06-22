/*
 * Angular equivalent model in TypeScript of a DTO for Nominal Compositions.
 * 
 * NOTE: It could be a class if there was the need for methods or 
 *  forinstantiating objects with logic.
 */
export interface NominalComposition {

  nominal_composition_name: string;
  description?: string;
  created_at: string;  // TODO: or Date?
  
}
