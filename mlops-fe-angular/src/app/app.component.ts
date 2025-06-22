/*
 * This file defines AppComponent, the most basic UI + logic + view encapsulation 
 * piece of this project, containing a template, styles, and logic.
 * 
 * This specific file is the root component logic (controller class). Used in 
 * bootstrapping (AppComponent). 
 */

import { Component } from '@angular/core';

@Component({
  selector: 'app-root', // Defines the tag <app-root></app-root> in the body of index.html
  templateUrl: './app.component.html', // Root component template (view); first content visible at /
  styleUrls: ['./app.component.css'] // Root component styles (can be empty); often overridden by global or component SCSS
})

export class AppComponent {
  title = 'mlops-fe-angular';
}
