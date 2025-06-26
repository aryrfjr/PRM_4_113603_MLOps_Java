/*
* Singleton service for DataOps related calls.
*/

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable, catchError, throwError } from 'rxjs';

import { Run } from '../models/run.model';

// TODO: Replace the hardcoded API_URL with Angular environment config.
const API_URL = 'http://localhost:8080/api/v1/dataops/generate';

@Injectable({ providedIn: 'root' })

export class DataOpsService {

  // TODO: Add error handling via catchError and throwError (for cleaner observables).

  constructor(private http: HttpClient) {}

  generate(nominalCompositionName: string, data: { numSimulations: number }): Observable<Run[]> {
    return this.http.post<Run[]>(`${API_URL}/${nominalCompositionName}`, data).pipe(
      catchError(this.handleError)
    );
  }

  private handleError(err: any): Observable<never> {

    console.error('API error:', err);

    return throwError(() => err);
    
  }

}
