import{PolymerElement,html}from"./my-app.js";class MyView1 extends PolymerElement{static get template(){return html`
      <style include="shared-styles">
        :host {
          display: block;

          padding: 10px;
        }
      </style>

      <div class="card">
        <div class="circle">1</div>
        <h1>View One</h1>
        <p>Ut labores minimum atomorum pro. Laudem tibique ut has.</p>
        <p>Lorem ipsum dolor sit amet, per in nusquam nominavi periculis, sit elit oportere ea.Lorem ipsum dolor sit amet, per in nusquam nominavi periculis, sit elit oportere ea.Cu mei vide viris gloriatur, at populo eripuit sit.</p>
      </div>
    `}connectedCallback(){fetch("http://localhost:8080/rs/greeting?name=carl").then(function(response){return response.json()}).then(function(j){console.log(j)})}}window.customElements.define("my-view1",MyView1);