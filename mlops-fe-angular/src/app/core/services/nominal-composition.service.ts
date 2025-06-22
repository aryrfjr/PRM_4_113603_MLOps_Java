/*
* Service to fetch Nominal Compositions.
*/

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs';

import { NominalComposition } from '../models/nominal-composition.model';

const API_URL = 'http://localhost:8080/api/v1/crud/nominal_compositions';

@Injectable({ providedIn: 'root' }) // Singleton (same instance will be injected over the whole app)

export class NominalCompositionService {

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
    return this.http.post<NominalComposition>(API_URL, data);
  }

}
