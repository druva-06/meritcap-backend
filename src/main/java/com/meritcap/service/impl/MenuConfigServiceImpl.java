package com.meritcap.service.impl;

import com.meritcap.DTOs.responseDTOs.menu.MenuConfigResponseDto;
import com.meritcap.DTOs.responseDTOs.menu.MenuItemDto;
import com.meritcap.DTOs.responseDTOs.permission.PermissionResponseDto;
import com.meritcap.DTOs.responseDTOs.user.UserPermissionsResponseDto;
import com.meritcap.exception.NotFoundException;
import com.meritcap.model.MenuPermission;
import com.meritcap.model.User;
import com.meritcap.repository.MenuPermissionRepository;
import com.meritcap.repository.UserRepository;
import com.meritcap.security.AuthenticatedUserResolver;
import com.meritcap.service.MenuConfigService;
import com.meritcap.service.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MenuConfigServiceImpl implements MenuConfigService {

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MenuPermissionRepository menuPermissionRepository;

    @Autowired
    private AuthenticatedUserResolver authenticatedUserResolver;

    @Override
    public MenuConfigResponseDto getMenuConfigForUser(Long userId) {
        log.info("Building menu configuration for user ID: {}", userId);

        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));

        // Get user permissions
        UserPermissionsResponseDto userPermissions = permissionService.getUserPermissions(userId);

        // Extract permission names
        Set<String> permissionNames = userPermissions.getAllPermissions().stream()
                .map(PermissionResponseDto::getName)
                .collect(Collectors.toSet());

        // Build menu based on role and permissions
        List<MenuItemDto> menuItems = buildMenuItems(user.getRole().getName(), permissionNames);

        return MenuConfigResponseDto.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .roleName(user.getRole().getName())
                .menuItems(menuItems)
                .allPermissions(new ArrayList<>(permissionNames))
                .build();
    }

    @Override
    public MenuConfigResponseDto getMenuConfigForCurrentUser() {
        Long currentUserId = authenticatedUserResolver.resolveCurrentUserId();
        return getMenuConfigForUser(currentUserId);
    }

    /**
     * Build menu items based on role and permissions
     */
    private List<MenuItemDto> buildMenuItems(String roleName, Set<String> permissions) {
        List<MenuItemDto> menuItems = new ArrayList<>();

        String roleUpper = roleName.toUpperCase();

        // Dashboard - visible to all
        menuItems.add(MenuItemDto.builder()
                .id("dashboard")
                .label("Dashboard")
                .icon("Home")
                .href("/admin/dashboard")
                .visible(true)
                .requiredPermissions(Collections.emptyList())
                .roleSpecificView(getRoleSpecificDashboardView(roleUpper))
                .build());

        // Leads - ADMIN, COUNSELOR
        if (hasAnyPermission(permissions, "MENU_LEADS", "LEAD_VIEW_ALL", "LEAD_VIEW_ASSIGNED")) {
            menuItems.add(MenuItemDto.builder()
                    .id("leads")
                    .label("Leads")
                    .icon("Users")
                    .href("/admin/leads")
                    .visible(true)
                    .requiredPermissions(Arrays.asList("LEAD_VIEW_ALL", "LEAD_VIEW_ASSIGNED"))
                    .features(buildLeadFeatures(permissions))
                    .build());
        }

        // AI Calling - ADMIN only
        if (hasPermission(permissions, "MENU_AI_CALLING") || roleUpper.equals("ADMIN")) {
            menuItems.add(MenuItemDto.builder()
                    .id("ai-calling")
                    .label("AI Calling")
                    .icon("Phone")
                    .href("/admin/ai-calling")
                    .visible(true)
                    .requiredPermissions(Arrays.asList("MENU_AI_CALLING"))
                    .build());
        }

        // Students - ADMIN, COUNSELOR, COLLEGE
        if (hasAnyPermission(permissions, "STUDENT_VIEW_ALL", "STUDENT_VIEW_ASSIGNED")) {
            menuItems.add(MenuItemDto.builder()
                    .id("students")
                    .label("Students")
                    .icon("GraduationCap")
                    .href("/admin/students")
                    .visible(true)
                    .requiredPermissions(Arrays.asList("STUDENT_VIEW_ALL", "STUDENT_VIEW_ASSIGNED"))
                    .features(buildStudentFeatures(permissions))
                    .build());
        }

        // Applications - ADMIN, COUNSELOR, COLLEGE, STUDENT
        if (hasAnyPermission(permissions, "APPLICATION_VIEW_ALL", "APPLICATION_VIEW_ASSIGNED",
                "COLLEGE_VIEW_OWN_APPLICATIONS")) {
            menuItems.add(MenuItemDto.builder()
                    .id("applications")
                    .label("Applications")
                    .icon("FileText")
                    .href("/admin/applications")
                    .visible(true)
                    .requiredPermissions(Arrays.asList("APPLICATION_VIEW_ALL", "APPLICATION_VIEW_ASSIGNED",
                            "COLLEGE_VIEW_OWN_APPLICATIONS"))
                    .features(buildApplicationFeatures(permissions))
                    .build());
        }

        // Community - ADMIN, COUNSELOR
        if (hasPermission(permissions, "MENU_COMMUNITY") ||
                hasAnyPermission(permissions, "COMMUNICATION_SEND_EMAIL", "COMMUNICATION_SEND_SMS")) {
            menuItems.add(MenuItemDto.builder()
                    .id("community")
                    .label("Community")
                    .icon("MessageSquare")
                    .href("/admin/community")
                    .visible(true)
                    .requiredPermissions(Arrays.asList("MENU_COMMUNITY"))
                    .build());
        }

        // Colleges menu with submenu
        if (hasPermission(permissions, "MENU_COLLEGES") ||
                hasAnyPermission(permissions, "COLLEGE_VIEW_ALL", "COLLEGE_VIEW_OWN_APPLICATIONS")) {
            menuItems.add(buildCollegesMenu(roleUpper, permissions));
        }

        // Partners - ADMIN only
        if (hasPermission(permissions, "MENU_PARTNERS") || roleUpper.equals("ADMIN")) {
            menuItems.add(buildPartnersMenu(permissions));
        }

        // Marketing - ADMIN only
        if (hasPermission(permissions, "MENU_MARKETING") || roleUpper.equals("ADMIN")) {
            menuItems.add(buildMarketingMenu(permissions));
        }

        // Finance - ADMIN only
        if (hasPermission(permissions, "MENU_FINANCE") || roleUpper.equals("ADMIN")) {
            menuItems.add(buildFinanceMenu(permissions));
        }

        // HR - ADMIN only
        if (hasPermission(permissions, "MENU_HR") || roleUpper.equals("ADMIN")) {
            menuItems.add(buildHRMenu(permissions));
        }

        // Assets - ADMIN only
        if (hasPermission(permissions, "MENU_ASSETS") || roleUpper.equals("ADMIN")) {
            menuItems.add(MenuItemDto.builder()
                    .id("assets")
                    .label("Assets")
                    .icon("Laptop")
                    .href("/admin/assets")
                    .visible(true)
                    .requiredPermissions(Arrays.asList("MENU_ASSETS"))
                    .build());
        }

        // Reports - ADMIN, COLLEGE (with analytics)
        if (hasPermission(permissions, "MENU_REPORTS") ||
                hasAnyPermission(permissions, "REPORT_VIEW_ALL", "COLLEGE_VIEW_ANALYTICS")) {
            menuItems.add(MenuItemDto.builder()
                    .id("reports")
                    .label("Reports")
                    .icon("BarChart3")
                    .href("/admin/reports")
                    .visible(true)
                    .requiredPermissions(Arrays.asList("MENU_REPORTS", "REPORT_VIEW_ALL", "COLLEGE_VIEW_ANALYTICS"))
                    .features(buildReportFeatures(permissions))
                    .build());
        }

        // Roles & Permissions - ADMIN only
        if (hasPermission(permissions, "MENU_ROLES_PERMISSIONS") ||
                hasAnyPermission(permissions, "USER_MANAGE_ROLES", "USER_MANAGE_PERMISSIONS") ||
                roleUpper.equals("ADMIN")) {
            menuItems.add(buildRolesPermissionsMenu(permissions));
        }

        // Settings - All users
        menuItems.add(MenuItemDto.builder()
                .id("settings")
                .label("Settings")
                .icon("Settings")
                .href("/admin/settings")
                .visible(true)
                .requiredPermissions(Collections.emptyList())
                .build());

        return menuItems;
    }

    private MenuItemDto buildCollegesMenu(String role, Set<String> permissions) {
        List<MenuItemDto.SubMenuItemDto> submenu = new ArrayList<>();

        if (role.equals("ADMIN")) {
            submenu.add(MenuItemDto.SubMenuItemDto.builder()
                    .id("all-colleges").label("All Colleges").href("/admin/colleges").visible(true).build());
            submenu.add(MenuItemDto.SubMenuItemDto.builder()
                    .id("partner-colleges").label("Partner Colleges").href("/admin/colleges/partners").visible(true)
                    .build());
            submenu.add(MenuItemDto.SubMenuItemDto.builder()
                    .id("partnership-performance").label("Partnership Performance").href("/admin/colleges/performance")
                    .visible(true).build());
            submenu.add(MenuItemDto.SubMenuItemDto.builder()
                    .id("commission-structure").label("Commission Structure").href("/admin/colleges/commission")
                    .visible(true).build());
            submenu.add(MenuItemDto.SubMenuItemDto.builder()
                    .id("course-catalog").label("Course Catalog").href("/admin/colleges/courses").visible(true)
                    .build());
            submenu.add(MenuItemDto.SubMenuItemDto.builder()
                    .id("intake-calendar").label("Intake Calendar").href("/admin/colleges/intakes").visible(true)
                    .build());
            submenu.add(MenuItemDto.SubMenuItemDto.builder()
                    .id("college-accounts").label("College Accounts").href("/admin/colleges/accounts").visible(true)
                    .build());
        } else if (role.equals("COLLEGE")) {
            submenu.add(MenuItemDto.SubMenuItemDto.builder()
                    .id("my-college").label("My College").href("/admin/colleges/my-college").visible(true).build());
            submenu.add(MenuItemDto.SubMenuItemDto.builder()
                    .id("course-catalog").label("Course Catalog").href("/admin/colleges/courses").visible(true)
                    .build());
            if (hasPermission(permissions, "COLLEGE_VIEW_ANALYTICS")) {
                submenu.add(MenuItemDto.SubMenuItemDto.builder()
                        .id("analytics").label("Analytics").href("/admin/colleges/analytics").visible(true).build());
            }
        }

        return MenuItemDto.builder()
                .id("colleges")
                .label("Colleges")
                .icon("Building2")
                .href("/admin/colleges")
                .visible(true)
                .submenu(submenu)
                .build();
    }

    private MenuItemDto buildPartnersMenu(Set<String> permissions) {
        List<MenuItemDto.SubMenuItemDto> submenu = Arrays.asList(
                MenuItemDto.SubMenuItemDto.builder().id("partners-overview").label("Overview").href("/admin/partners")
                        .visible(true).build(),
                MenuItemDto.SubMenuItemDto.builder().id("college-partners").label("College Partners")
                        .href("/admin/partners/colleges").visible(true).build(),
                MenuItemDto.SubMenuItemDto.builder().id("subagent-partners").label("Sub-Agent Partners")
                        .href("/admin/partners/subagents").visible(true).build());

        return MenuItemDto.builder().id("partners").label("Partners").icon("UserCheck").href("/admin/partners")
                .visible(true).submenu(submenu).build();
    }

    private MenuItemDto buildMarketingMenu(Set<String> permissions) {
        List<MenuItemDto.SubMenuItemDto> submenu = Arrays.asList(
                MenuItemDto.SubMenuItemDto.builder().id("marketing-overview").label("Overview").href("/admin/marketing")
                        .visible(true).build(),
                MenuItemDto.SubMenuItemDto.builder().id("offline-marketing").label("Offline Marketing")
                        .href("/admin/marketing/offline").visible(true).build(),
                MenuItemDto.SubMenuItemDto.builder().id("webinars").label("Webinars").href("/admin/marketing/webinars")
                        .visible(true).build(),
                MenuItemDto.SubMenuItemDto.builder().id("social-media").label("Social Media")
                        .href("/admin/marketing/social").visible(true).build(),
                MenuItemDto.SubMenuItemDto.builder().id("digital-campaigns").label("Digital Campaigns")
                        .href("/admin/marketing/digital").visible(true).build(),
                MenuItemDto.SubMenuItemDto.builder().id("content-marketing").label("Content Marketing")
                        .href("/admin/marketing/content").visible(true).build(),
                MenuItemDto.SubMenuItemDto.builder().id("partner-marketing").label("Partner Marketing")
                        .href("/admin/marketing/partner").visible(true).build());

        return MenuItemDto.builder().id("marketing").label("Marketing").icon("Megaphone").href("/admin/marketing")
                .visible(true).submenu(submenu).build();
    }

    private MenuItemDto buildFinanceMenu(Set<String> permissions) {
        List<MenuItemDto.SubMenuItemDto> submenu = Arrays.asList(
                MenuItemDto.SubMenuItemDto.builder().id("finance-overview").label("Overview").href("/admin/finance")
                        .visible(true).build(),
                MenuItemDto.SubMenuItemDto.builder().id("invoices").label("Invoice Generation")
                        .href("/admin/finance/invoices").visible(true).build(),
                MenuItemDto.SubMenuItemDto.builder().id("expenses").label("Expense Management")
                        .href("/admin/finance/expenses").visible(true).build());

        return MenuItemDto.builder().id("finance").label("Finance").icon("DollarSign").href("/admin/finance")
                .visible(true).submenu(submenu).build();
    }

    private MenuItemDto buildHRMenu(Set<String> permissions) {
        List<MenuItemDto.SubMenuItemDto> submenu = Arrays.asList(
                MenuItemDto.SubMenuItemDto.builder().id("hr-overview").label("Overview").href("/admin/hr").visible(true)
                        .build(),
                MenuItemDto.SubMenuItemDto.builder().id("leave-management").label("Leave Management")
                        .href("/admin/hr/leave").visible(true).build(),
                MenuItemDto.SubMenuItemDto.builder().id("attendance").label("Attendance").href("/admin/hr/attendance")
                        .visible(true).build(),
                MenuItemDto.SubMenuItemDto.builder().id("training").label("Training Records").href("/admin/hr/training")
                        .visible(true).build());

        return MenuItemDto.builder().id("hr").label("HR").icon("UserCog").href("/admin/hr").visible(true)
                .submenu(submenu).build();
    }

    private MenuItemDto buildRolesPermissionsMenu(Set<String> permissions) {
        List<MenuItemDto.SubMenuItemDto> submenu = Arrays.asList(
                MenuItemDto.SubMenuItemDto.builder().id("roles-management").label("Roles Management")
                        .href("/admin/roles").visible(true).build(),
                MenuItemDto.SubMenuItemDto.builder().id("permissions-management").label("Permissions")
                        .href("/admin/permissions").visible(true).build(),
                MenuItemDto.SubMenuItemDto.builder().id("user-permissions").label("User Permissions")
                        .href("/admin/user-permissions").visible(true).build());

        return MenuItemDto.builder().id("roles").label("Roles & Permissions").icon("Shield").href("/admin/roles")
                .visible(true).submenu(submenu).build();
    }

    private Map<String, Boolean> buildLeadFeatures(Set<String> permissions) {
        Map<String, Boolean> features = new HashMap<>();
        features.put("create", hasPermission(permissions, "LEAD_CREATE"));
        features.put("edit", hasPermission(permissions, "LEAD_EDIT"));
        features.put("delete", hasPermission(permissions, "LEAD_DELETE"));
        features.put("assign", hasPermission(permissions, "LEAD_ASSIGN"));
        features.put("transfer", hasPermission(permissions, "LEAD_TRANSFER"));
        features.put("bulkAssign", hasPermission(permissions, "LEAD_BULK_ASSIGN"));
        features.put("export", hasPermission(permissions, "LEAD_EXPORT"));
        features.put("import", hasPermission(permissions, "LEAD_IMPORT"));
        return features;
    }

    private Map<String, Boolean> buildStudentFeatures(Set<String> permissions) {
        Map<String, Boolean> features = new HashMap<>();
        features.put("create", hasPermission(permissions, "STUDENT_CREATE"));
        features.put("edit", hasPermission(permissions, "STUDENT_EDIT"));
        features.put("delete", hasPermission(permissions, "STUDENT_DELETE"));
        features.put("export", hasPermission(permissions, "STUDENT_EXPORT"));
        features.put("viewAll", hasPermission(permissions, "STUDENT_VIEW_ALL"));
        features.put("viewAssigned", hasPermission(permissions, "STUDENT_VIEW_ASSIGNED"));
        return features;
    }

    private Map<String, Boolean> buildApplicationFeatures(Set<String> permissions) {
        Map<String, Boolean> features = new HashMap<>();
        features.put("create", hasPermission(permissions, "APPLICATION_CREATE"));
        features.put("edit", hasPermission(permissions, "APPLICATION_EDIT"));
        features.put("delete", hasPermission(permissions, "APPLICATION_DELETE"));
        features.put("approve", hasPermission(permissions, "APPLICATION_APPROVE"));
        features.put("export", hasPermission(permissions, "APPLICATION_EXPORT"));
        features.put("viewAll", hasPermission(permissions, "APPLICATION_VIEW_ALL"));
        features.put("viewAssigned", hasPermission(permissions, "APPLICATION_VIEW_ASSIGNED"));
        features.put("reviewApplications", hasPermission(permissions, "COLLEGE_REVIEW_APPLICATIONS"));
        return features;
    }

    private Map<String, Boolean> buildReportFeatures(Set<String> permissions) {
        Map<String, Boolean> features = new HashMap<>();
        features.put("viewAll", hasPermission(permissions, "REPORT_VIEW_ALL"));
        features.put("export", hasPermission(permissions, "REPORT_EXPORT"));
        features.put("analytics", hasPermission(permissions, "REPORT_ANALYTICS"));
        features.put("collegeAnalytics", hasPermission(permissions, "COLLEGE_VIEW_ANALYTICS"));
        return features;
    }

    private String getRoleSpecificDashboardView(String role) {
        switch (role) {
            case "ADMIN":
            case "SUPER_ADMIN":
                return "ADMIN";
            case "COLLEGE":
                return "COLLEGE";
            case "COUNSELOR":
            case "COUNSELLOR":
                return "COUNSELOR";
            case "SUB_AGENT":
            case "SUBAGENT":
                return "SUBAGENT";
            case "STUDENT":
                return "STUDENT";
            default:
                return "ADMIN";
        }
    }

    private boolean hasPermission(Set<String> permissions, String permission) {
        return permissions.contains(permission);
    }

    private boolean hasAnyPermission(Set<String> permissions, String... requiredPermissions) {
        return Arrays.stream(requiredPermissions)
                .anyMatch(permissions::contains);
    }

    /**
     * Get required permissions for a menu from database
     * 
     * @param menuId    Menu identifier
     * @param submenuId Submenu identifier (null for main menu)
     * @return List of permission names required
     */
    private List<String> getRequiredPermissionsFromDB(String menuId, String submenuId) {
        List<MenuPermission> menuPermissions = menuPermissionRepository
                .findByMenuIdAndSubmenuId(menuId, submenuId);

        return menuPermissions.stream()
                .map(mp -> mp.getPermission().getName())
                .collect(Collectors.toList());
    }

    /**
     * Check if user has access to menu based on database permissions
     * 
     * @param menuId          Menu identifier
     * @param submenuId       Submenu identifier (null for main menu)
     * @param userPermissions User's permission set
     * @return true if user has at least one of the required permissions
     */
    private boolean hasMenuAccess(String menuId, String submenuId, Set<String> userPermissions) {
        List<String> requiredPermissions = getRequiredPermissionsFromDB(menuId, submenuId);

        // If no permissions configured, deny access (secure by default)
        if (requiredPermissions.isEmpty()) {
            return false;
        }

        // Check if user has any of the required permissions (OR logic)
        return requiredPermissions.stream()
                .anyMatch(userPermissions::contains);
    }
}
