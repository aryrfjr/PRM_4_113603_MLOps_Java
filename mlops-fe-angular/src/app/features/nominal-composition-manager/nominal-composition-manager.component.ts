/* 
 Nominal Composition Manager that performs CRUD via HTTP to FastAPI backend.
 */

import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-nominal-manager',
  templateUrl: './nominal-composition-manager.component.html'
})

export class NominalCompositionManagerComponent implements OnInit {

  API_URL = 'http://localhost:8000/api/v1/nominal_compositions'; // TODO: the correct URL
  compositions: any[] = [];
  selected = '';
  name = '';
  description = '';
  mode: 'Read' | 'Create' | 'Update' | 'Delete' = 'Read';

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.fetchCompositions();
  }

  fetchCompositions() {
    this.http.get<any[]>(this.API_URL).subscribe((data) => (this.compositions = data));
  }

  create() {
    this.http.post(this.API_URL + '/', { name: this.name, description: this.description }).subscribe(() => {
      alert('Created');
      this.fetchCompositions();
    });
  }

  update() {
    this.http.put(this.API_URL + '/' + this.selected, { name: this.name, description: this.description }).subscribe(() => {
      alert('Updated');
      this.fetchCompositions();
    });
  }

  delete() {
    this.http.delete(this.API_URL + '/' + this.selected).subscribe(() => {
      alert('Deleted');
      this.fetchCompositions();
    });

  }
}
