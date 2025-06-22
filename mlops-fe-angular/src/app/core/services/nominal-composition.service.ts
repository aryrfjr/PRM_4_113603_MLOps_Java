/*
* Service to fetch Nominal Compositions.
*/

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { NominalComposition } from '../models/nominal-composition.model';

const API_URL = 'http://localhost:8080/api/v1/crud/nominal_compositions';

@Injectable({ providedIn: 'root' }) // Singleton

export class NominalCompositionService {

  constructor(private http: HttpClient) {}

  getAll(): Observable<NominalComposition[]> {

    return this.http.get<NominalComposition[]>(API_URL);

  }

}
