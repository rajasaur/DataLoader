<%--
/* Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author <a href='mailto:limcheekin@vobject.com'>Lim Chee Kin</a>
 *
 * @since 0.1
 */
 --%>
<%@ page import="grails.persistence.Event" %>
<%@ page import="org.codehaus.groovy.grails.scaffolding.DomainClassPropertyComparator" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: '${domainClass.propertyName}.label', default: domainClass.name)}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/fileUpload/')}"><g:message code="default.home.label"/></a></span>
            <span class="menuButton"><g:link class="create" action="create" params="[dc:params.dc]"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.list.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        <%  excludedProps = Event.allEvents.toList() << 'version'
                            allowedNames = domainClass.persistentProperties*.name << 'id' << 'dateCreated' << 'lastUpdated'
                            props = domainClass.properties.findAll { allowedNames.contains(it.name) && !excludedProps.contains(it.name) && !Collection.isAssignableFrom(it.type) }
                            props.sort(new DomainClassPropertyComparator(domainClass))
                            props.eachWithIndex { p, i ->
                                if (i < 6) {
                                    if (p.isAssociation()) { %>
                            <th><g:message code="${domainClass.propertyName}.${p.name}.label" default="${p.naturalName}" /></th>
                        <%      } else { %>
                            <g:sortableColumn property="${p.name}" title="${message(code: '${domainClass.propertyName}.${p.name}.label', default: p.naturalName)}" params="[dc:params.dc]" />
                        <%  }   }   } %>
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${domainInstanceList}" status="i" var="domainInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        <%  props.eachWithIndex { p, j ->
                                if (j == 0) { %>
                            <td><g:link action="show" id="${domainInstance.id}" params="[dc:params.dc]">${fieldValue(bean: domainInstance, field: p.name)}</g:link></td>
                        <%      } else if (j < 6) {
                                    if (p.type == Boolean.class || p.type == boolean.class) { %>
                            <td><g:formatBoolean boolean="domainInstance.${p.name}" /></td>
                        <%          } else if (p.type == Date.class || p.type == java.sql.Date.class || p.type == java.sql.Time.class || p.type == Calendar.class) { %>
                            <td><g:formatDate date="domainInstance.${p.name}" /></td>
                        <%          } else { %>
                            <td>${fieldValue(bean: domainInstance, field: p.name)}</td>
                        <%  }   }   } %>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${domainInstanceTotal}" params="[dc:params.dc]" />
            </div>
        </div>
    </body>
</html>
