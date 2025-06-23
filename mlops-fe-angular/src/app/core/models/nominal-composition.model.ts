/*
 * Angular equivalent model in TypeScript of a DTO for Nominal Compositions.
 * 
 * NOTE: It could be a class if there was the need for methods or 
 *  forinstantiating objects with logic.
 */
export interface NominalComposition {

  /*
  * NOTE: In TypeScript, the question mark (?) after a property name means 
  * that the property is optional.
  */
  name: string;
  description?: string;
  created_at: string;  // TODO: or Date?
  updated_at: string;  // TODO: or Date?
  created_by: string;
  updated_by: string;
  
}
