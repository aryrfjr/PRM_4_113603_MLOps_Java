/*
* Singleton service to manage SimulationArtifacts.
*/

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs';

import { SimulationArtifact } from '../models/simulation-artifact.model';

// TODO: Replace the hardcoded API_URL with Angular environment config.
const API_URL = 'http://localhost:8080/api/v1/crud/simulation_artifacts';

@Injectable({ providedIn: 'root' })

export class SimulationArtifactService {

  // TODO: Add error handling via catchError and throwError (for cleaner observables).

  constructor(private http: HttpClient) {}

  getAll(subRunId: number): Observable<SimulationArtifact[]> {
    return this.http.get<SimulationArtifact[]>(`${API_URL}?subRunId=${subRunId}`);
  }

}
