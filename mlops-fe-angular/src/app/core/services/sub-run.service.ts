/*
* Singleton service to manage SubRuns.
*/

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs';

import { SubRun } from '../models/sub-run.model';

// TODO: Replace the hardcoded API_URL with Angular environment config.
const API_URL = 'http://localhost:8080/api/v1/crud/sub_runs';

@Injectable({ providedIn: 'root' })

export class SubRunService {

  // TODO: Add error handling via catchError and throwError (for cleaner observables).

  constructor(private http: HttpClient) {}

  getAll(runId: number): Observable<SubRun[]> {
    return this.http.get<SubRun[]>(`${API_URL}?runId=${runId}`);
  }

}
