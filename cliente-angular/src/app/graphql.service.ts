import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { AuthPayload, User, UserRelationDetail } from './models';

@Injectable({ providedIn: 'root' })
export class GraphqlService {
  private readonly endpoint = 'http://localhost:8080/graphql';

  constructor(private http: HttpClient) {}

  login(username: string, password: string): Observable<AuthPayload> {
    const query = `
      query Login($username: String!, $password: String!) {
        login(username: $username, password: $password) {
          authenticated
          token
          message
          user {
            id username firstName lastName email phone profession description avatarUrl
          }
        }
      }
    `;
    return this.http.post<any>(this.endpoint, { query, variables: { username, password } })
      .pipe(map(response => response.data.login));
  }

  loadPanel(userId: string, token: string): Observable<{me: User; userRelations: UserRelationDetail[]}> {
    const query = `
      query Panel($userId: ID!) {
        me(userId: $userId) {
          id username firstName lastName email phone profession description avatarUrl
        }
        userRelations(userId: $userId) {
          type
          status
          user {
            id username firstName lastName email phone profession description avatarUrl
          }
        }
      }
    `;
    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });
    return this.http.post<any>(this.endpoint, { query, variables: { userId } }, { headers })
      .pipe(map(response => response.data));
  }
}
