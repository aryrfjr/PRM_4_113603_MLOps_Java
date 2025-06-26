/*
* Singleton service for CRUD operations for Nominal Compositions.
*/

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable, catchError, throwError } from 'rxjs';

import { NominalComposition } from '../models/nominal-composition.model';

// TODO: Replace the hardcoded API_URL with Angular environment config.
const API_URL = 'http://localhost:8080/api/v1/crud/nominal_compositions';

@Injectable({ providedIn: 'root' }) // Singleton (same instance will be injected over the whole app)

export class NominalCompositionService {

  // TODO: Add error handling via catchError and throwError (for cleaner observables).

  constructor(private http: HttpClient) {}

  /*
  * NOTE: Observable is used here simply because HTTP requests are asynchronous.
  *       The call to this.http.get() does not return data immediately (because 
  *       the backend takes time to respond). So, instead, it returns an Observable 
  *       that will eventually emit the data when it's ready.
  * 
  * NOTE: This lets Angular handle the delay without blocking the app’s UI or 
  *       freezing the browser.
  */
  getAll(): Observable<NominalComposition[]> {
    return this.http.get<NominalComposition[]>(API_URL);
  }

  create(data: { name: string; description?: string }): Observable<NominalComposition> {
    return this.http.post<NominalComposition>(API_URL, data).pipe(
      catchError(this.handleError)
    );
  }

  update(name: string, data: { description?: string }): Observable<NominalComposition> {
    return this.http.put<NominalComposition>(`${API_URL}/${name}`, data);
  }

  delete(name: string): Observable<void> {
    return this.http.delete<void>(`${API_URL}/${name}`).pipe(
      catchError(this.handleError)
    );
  }

  private handleError(err: any): Observable<never> {

    console.error('API error:', err);

    return throwError(() => err);
    
  }

}
