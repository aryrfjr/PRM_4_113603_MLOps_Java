/*
* Singleton service to manage Runs.
*/

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs';

import { Run } from '../models/run.model';

// TODO: Replace the hardcoded API_URL with Angular environment config.
const API_URL = 'http://localhost:8080/api/v1/crud/runs';

@Injectable({ providedIn: 'root' })

export class RunService {

  // TODO: Add error handling via catchError and throwError (for cleaner observables).

  constructor(private http: HttpClient) {}

  getAll(nominalCompositionId: number): Observable<Run[]> {
    return this.http.get<Run[]>(`${API_URL}?nominalCompositionId=${nominalCompositionId}`);
  }

}
