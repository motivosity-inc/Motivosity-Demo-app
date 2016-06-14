(function (app) {

	app.AppComponent = ng.core.Component({
			selector: 'demo-app',
			template: "<h1>Motivosity- OAuth Demo</h1> " +
			"<button (click)='getUserList()'>List Motivosity Users</button><br>" +
			"<a href='/api/authorize'>Authorize Test App at Motivosity</a>" +
			"<ul><li *ngFor='let user of userList'>{{user.name}}</li></ul>"
		})
		.Class({
			constructor: function () {
				this.userList = [];

				this.getUserList = function () {
					var request = new XMLHttpRequest();
					request.open("GET", "/api/motivosity/userlist", false);
					request.send(null);
					this.userList = JSON.parse(request.responseText);
				}
			}
		});

})(window.app || (window.app = {}));