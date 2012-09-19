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
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/fileUpload/')}"><g:message code="default.home.label"/></a></span>
            <span class="menuButton"><g:link class="list" action="list" params="[dc:params.dc]"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
            <span class="menuButton"><g:link class="create" action="create" params="[dc:params.dc]"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
        </div>
        <div class="body">
            <h1><g:message code="default.show.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>
                    <%  excludedProps = Event.allEvents.toList() << 'version'
                        allowedNames = domainClass.persistentProperties*.name << 'id' << 'dateCreated' << 'lastUpdated'
                        props = domainClass.properties.findAll { allowedNames.contains(it.name) && !excludedProps.contains(it.name) }
                        props.sort(new DomainClassPropertyComparator(domainClass))
                        props.each { p -> %>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="${domainClass.propertyName}.${p.name}.label" default="${p.naturalName}" /></td>
                            <%  if (p.isEnum()) { %>
                            <td valign="top" class="value">${fieldValue(bean: domainInstance, field: p.name)}</td>
                            <%  } else if (p.oneToMany || p.manyToMany) { %>
                            <td valign="top" style="text-align: left;" class="value">
                                <ul>
                                <% items = domainInstance."$p.name" 
                                   items.each { item -> %>
                                    <li><g:link controller="ddc" action="show" id="${item.id}" params="[dc:p.referencedDomainClass.fullName]">${item?.encodeAsHTML()}</g:link></li>
                                                                <% } %>
                                </ul>
                            </td>
                            <%  } else if (p.manyToOne || p.oneToOne) { 
                                  item = domainInstance."$p.name" %>
                            <td valign="top" class="value"><g:link controller="ddc" action="show" id="${item.id}" params="[dc:p.referencedDomainClass.fullName]">${item?.encodeAsHTML()}</g:link></td>
                            <%  } else if (p.type == Boolean.class || p.type == boolean.class) { 
                                  value = domainInstance."$p.name" %>
                            <td valign="top" class="value"><g:formatBoolean boolean="${value}" /></td>
                            <%  } else if (p.type == Date.class || p.type == java.sql.Date.class || p.type == java.sql.Time.class || p.type == Calendar.class) { 
                                  value = domainInstance."$p.name" %>
                            <td valign="top" class="value"><g:formatDate date="${value}" /></td>
                            <%  } else if(!p.type.isArray()) { %>
                            <td valign="top" class="value">${fieldValue(bean: domainInstance, field: p.name)}</td>
                            <%  } %>
                        </tr>
                    <%  } %>
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <g:hiddenField name="id" value="${domainInstance?.id}" />
                    <g:hiddenField name="dc" value="${params.dc}" />
                    <span class="button"><g:actionSubmit class="edit" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" /></span>
                    <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
