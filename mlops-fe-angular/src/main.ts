/*
 * This is the entry point for the Angular application. 
 */

// Angular modules from external packages reusable across all Angular apps.
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

// Modules specific to this project defined in local files.
import { AppModule } from './app/app.module';

// This is will create and bootstrap the Angular application on a web page.
// 
// It's primarily used with Just-In-Time (JIT) compilation and is the 
// standard way to launch an application in a browser environment.
// 
// It returns a PlatformRef object, which represents the platform on 
// which the application runs; in this case, the main module for this Angular app.
platformBrowserDynamic().bootstrapModule(AppModule)
  .catch(err => console.error(err));
